package software.amazon.opensearchserverless.accesspolicy;

import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.opensearchserverless.OpenSearchServerlessClient;
import software.amazon.awssdk.services.opensearchserverless.model.ConflictException;
import software.amazon.awssdk.services.opensearchserverless.model.CreateAccessPolicyRequest;
import software.amazon.awssdk.services.opensearchserverless.model.CreateAccessPolicyResponse;
import software.amazon.awssdk.services.opensearchserverless.model.InternalServerException;
import software.amazon.awssdk.services.opensearchserverless.model.ValidationException;
import software.amazon.cloudformation.exceptions.CfnAlreadyExistsException;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnInternalFailureException;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

public class CreateHandler extends BaseHandlerStd {

    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final ProxyClient<OpenSearchServerlessClient> proxyClient,
            final Logger logger) {

        return proxy.initiate("AWS-OpenSearchServerless-AccessPolicy::Create", proxyClient, request.getDesiredResourceState(), callbackContext)
                    .translateToServiceRequest(Translator::translateToCreateRequest)
                    .makeServiceCall((awsRequest, client) -> createAccessPolicy(awsRequest, client, logger))
                    .done(awsResponse -> ProgressEvent.defaultSuccessHandler(Translator.translateFromCreateResponse(awsResponse)));
    }

    private CreateAccessPolicyResponse createAccessPolicy(
            final CreateAccessPolicyRequest createAccessPolicyRequest,
            final ProxyClient<OpenSearchServerlessClient> proxyClient,
            final Logger logger) {
        CreateAccessPolicyResponse createAccessPolicyResponse;
        try {
            createAccessPolicyResponse = proxyClient.injectCredentialsAndInvokeV2(createAccessPolicyRequest, proxyClient.client()::createAccessPolicy);
        } catch (ConflictException e) {
            throw new CfnAlreadyExistsException(e);
        } catch (ValidationException e) {
            throw new CfnInvalidRequestException(createAccessPolicyRequest.toString(), e);
        } catch (InternalServerException e) {
            throw new CfnInternalFailureException(e);
        } catch (AwsServiceException e) {
            throw new CfnGeneralServiceException(ResourceModel.TYPE_NAME, e);
        }
        logger.log(String.format("%s successfully created for %s", ResourceModel.TYPE_NAME, createAccessPolicyRequest));
        return createAccessPolicyResponse;
    }
}
