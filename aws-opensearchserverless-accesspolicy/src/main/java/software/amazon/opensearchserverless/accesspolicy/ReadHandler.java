package software.amazon.opensearchserverless.accesspolicy;

import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.opensearchserverless.OpenSearchServerlessClient;
import software.amazon.awssdk.services.opensearchserverless.model.GetAccessPolicyRequest;
import software.amazon.awssdk.services.opensearchserverless.model.GetAccessPolicyResponse;
import software.amazon.awssdk.services.opensearchserverless.model.InternalServerException;
import software.amazon.awssdk.services.opensearchserverless.model.ResourceNotFoundException;
import software.amazon.awssdk.services.opensearchserverless.model.ValidationException;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnInternalFailureException;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

public class ReadHandler extends BaseHandlerStd {

    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final ProxyClient<OpenSearchServerlessClient> proxyClient,
            final Logger logger) {

        return proxy.initiate("AWS-OpenSearchServerless-AccessPolicy::Read", proxyClient, request.getDesiredResourceState(), callbackContext)
                    .translateToServiceRequest(Translator::translateToReadRequest)
                    .makeServiceCall((awsRequest, client) -> getAccessPolicy(awsRequest, client, logger))
                    .done(awsResponse -> ProgressEvent.defaultSuccessHandler(Translator.translateFromReadResponse(awsResponse)));
    }

    private GetAccessPolicyResponse getAccessPolicy(
            final GetAccessPolicyRequest getAccessPolicyRequest,
            final ProxyClient<OpenSearchServerlessClient> proxyClient,
            final Logger logger) {

        GetAccessPolicyResponse getAccessPolicyResponse;
        try {
            getAccessPolicyResponse = proxyClient.injectCredentialsAndInvokeV2(getAccessPolicyRequest, proxyClient.client()::getAccessPolicy);
        } catch (ResourceNotFoundException e) {
            throw new CfnNotFoundException(e);
        } catch (ValidationException e) {
            throw new CfnInvalidRequestException(getAccessPolicyRequest.toString(), e);
        } catch (InternalServerException e) {
            throw new CfnInternalFailureException(e);
        } catch (final AwsServiceException e) {
            throw new CfnGeneralServiceException(ResourceModel.TYPE_NAME, e);
        }
        logger.log(String.format("%s has successfully been read.", ResourceModel.TYPE_NAME));
        return getAccessPolicyResponse;
    }
}