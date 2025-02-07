package it.dgsia.apigateway.exception;

import java.time.ZonedDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ErrorResponse {
	private String timestamp;
	private String path;
	private int status;
	private String error;
	private String requestId;

	public ErrorResponse(String path, int status, String error, String requestId) {
		this.timestamp = ZonedDateTime.now().toString();
		this.path = path;
		this.status = status;
		this.error = error;
		this.requestId = requestId;
	}
}