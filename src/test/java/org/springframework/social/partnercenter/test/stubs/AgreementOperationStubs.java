package org.springframework.social.partnercenter.test.stubs;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.anyUrl;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import org.springframework.social.partnercenter.api.agreement.Agreement;
import org.springframework.social.partnercenter.api.agreement.AgreementOperationsTest;
import org.springframework.social.partnercenter.api.agreement.AgreementType;
import org.springframework.social.partnercenter.serialization.JsonConverter;
import org.springframework.social.partnercenter.test.Resource;

import com.github.tomakehurst.wiremock.matching.UrlPattern;

public class AgreementOperationStubs {

	public static void given_confirmCustomerAcceptance_201_Created(Agreement agreement) {
		stubFor(post(anyUrl())
				        .willReturn(aResponse()
						                    .withStatus(200)
						                    .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						                    .withBody(new JsonConverter().toJson(agreement))));
	}

	public static void given_getConfirmations_200_OK() {
		stubFor(get(anyUrl())
				        .willReturn(aResponse()
						                    .withStatus(200)
						                    .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						                    .withBody(Resource.parseFile("data/agreement/get-confirmations.json").getAsString())));
	}

	public static void given_getConfirmations_forMicrosoftCloudAgreement_200_OK() {
		stubFor(get(UrlPattern.fromOneOf("/v1/customers/" + AgreementOperationsTest.CUSTOMER_TENANT_ID + "/agreements?AgreementType=" + AgreementType.MICROSOFT_CLOUD_AGREEMENT.jsonValue(), null, null, null))
			.willReturn(aResponse()
				.withStatus(200)
				.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.withBody(Resource.parseFile("data/agreement/get-confirmations.json").getAsString())));
	}
	
	public static void given_getConfirmations_forMicrosoftCustomerAgreement_200_OK() {
		stubFor(get(UrlPattern.fromOneOf("/v1/customers/" + AgreementOperationsTest.CUSTOMER_TENANT_ID + "/agreements?AgreementType=" + AgreementType.MICROSOFT_CUSTOMER_AGREEMENT.jsonValue(), null, null, null))
			.willReturn(aResponse()
				.withStatus(200)
				.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.withBody(Resource.parseFile("data/agreement/get-confirmations-for-MicrosoftCustomerAgreement.json").getAsString())));
	}
}
