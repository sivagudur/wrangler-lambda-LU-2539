package uk.gov.ons.validation.serverless;

import java.util.ArrayList;
import java.util.List;


import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import uk.gov.ons.validation.entity.QuestionInputData;
import uk.gov.ons.validation.entity.ValidationConfig;
import uk.gov.ons.validation.entity.WranglerRequest;
import uk.gov.ons.validation.entity.WranglerResponseData;
import uk.gov.ons.validation.entity.WranglerResponse;
import lombok.extern.log4j.Log4j2;
import com.amazonaws.services.lambda.AWSLambdaAsync;
import com.amazonaws.services.lambda.AWSLambdaAsyncClient;
import com.amazonaws.services.lambda.model.InvokeRequest;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.regions.Regions;
import uk.gov.ons.validation.util.PropertiesUtil;


@Log4j2
public class WranglerLambdaHandler implements RequestHandler<WranglerRequest, String> {

    private static final String SEND_MESSAGE = "Attempting to invoke %s with the json string %s.";

    @Override
    public String handleRequest(WranglerRequest request, Context context) {

        log.info("received Question input: {}", request);

        try {

            log.info("Parsing Json Array to Java object: ");

            //1. Building Validation Config
            List<ValidationConfig> configuration = new ArrayList<>();
            ValidationConfig config1 = new ValidationConfig();
            config1.setQuestionCode("601");
            config1.setDerivedQuestionCode("700");
            configuration.add(config1);
            ValidationConfig config2 = new ValidationConfig();
            config2.setQuestionCode("602");
            config2.setDerivedQuestionCode("701");
            configuration.add(config2);

            List<QuestionInputData> responses = request.getResponses();

            System.out.println("Request Data" + responses);

            if (responses != null) {
                for (QuestionInputData response : responses) {
                    System.out.println("response" + response);

                }

            }

            String finalQuestCode = null;
            String finalDerivedQuestCode = null;
            String finalQuestCodeValue = null;
            String finalDerivedQuestValue = null;
            List<WranglerResponseData> data = new ArrayList<>();


            if (configuration != null && configuration.size() > 0) {
                for (ValidationConfig config : configuration) {
                    String derivedCode = config.getDerivedQuestionCode();
                    System.out.println("Question Code in Validation Config " + config.getQuestionCode());

                    String derivedQuestValue = null;
                    String questionValue = null;
                    boolean isQuestionCodeFound = false;
                    boolean isDerivedQuestFound = false;
                    for (QuestionInputData inputData : responses) {


                        if (inputData.getQuestionCode().equals(config.getQuestionCode())) {
                            System.out.println("Matching object found for Question" + inputData.getQuestionCode());
                            questionValue = inputData.getResponse();
                            isQuestionCodeFound = true;
                            finalQuestCode = inputData.getQuestionCode();
                            finalQuestCodeValue = inputData.getResponse();
                        }
                        if (inputData.getQuestionCode().equals(config.getDerivedQuestionCode())) {
                            derivedQuestValue = inputData.getResponse();
                            isDerivedQuestFound = true;
                            finalDerivedQuestCode = inputData.getQuestionCode();
                            finalDerivedQuestValue = inputData.getResponse();
                            System.out.println("Matching object found for Derived Question" + config.getDerivedQuestionCode());
                        }
                    }
                    if (isQuestionCodeFound && isDerivedQuestFound) {
                        System.out.println("Match Found for both Question and Derived Question");
                        System.out.println("Question Code " + finalQuestCode + "Derived Question Code" + finalDerivedQuestCode);
                        System.out.println("Question Code Value " + finalQuestCodeValue + "Derived Question Code Value" + finalDerivedQuestValue);
                        //Call Larissa Lambda which performs Validation

                        WranglerResponseData dataElement = WranglerResponseData.builder()
                                .questionValue(finalQuestCodeValue)
                                .derivedQuestionValue(finalDerivedQuestValue)
                                .build();
                        sendDataToWrangler(dataElement);
                        data.add(dataElement);
                        finalQuestCode = null;
                        finalDerivedQuestCode = null;
                        finalQuestCodeValue = null;
                        finalDerivedQuestValue = null;

                    }

                }

            }



        } catch (Exception e) {
            log.error(e.getStackTrace());
            return "Failed";
        }


        return "Accepted ";
    }


    private void sendDataToWrangler(WranglerResponseData data) throws JsonProcessingException {
        try {
            AWSLambdaAsync client = AWSLambdaAsyncClient.asyncBuilder().withRegion(Regions.EU_WEST_2).build();

            WranglerResponse request = WranglerResponse.builder().responses(data).build();

            String requestJson = new ObjectMapper().writeValueAsString(request);
            String wranglerName = PropertiesUtil.getProperty(PropertiesUtil.WRANGLER_NAME);

            log.info(String.format(SEND_MESSAGE, wranglerName, requestJson));

            InvokeRequest invokeRequest = new InvokeRequest();
            invokeRequest.withFunctionName(wranglerName).withPayload(requestJson);

            client.invoke(invokeRequest);
        } catch (JsonProcessingException e) {
            log.error("An exception occurred whilst attempting to prepare and send the request.", e);
            throw e;
        }
    }
}
