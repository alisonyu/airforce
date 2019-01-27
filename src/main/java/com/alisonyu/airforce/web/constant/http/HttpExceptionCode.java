package com.alisonyu.airforce.web.constant.http;

/**
 * @author yuzhiyi
 * @date 2018/9/28 20:15
 */
public enum HttpExceptionCode {


	/**
	 * SUCCESS CODE
	 */

	/**
	 * 服务器已经成功处理请求
	 */
	OK(200),
	/**
	 * 请求成功并且服务器创建了新的资源
	 */
	CREATED(201),
	/**
	 * 服务器已经接受请求，但尚未处理
	 */
	ACCEPTED(202),
	/**
	 * 服务器已经成功处理请求，但是返回的信息可能是另外一个来源
	 */
	NON_AUTHORITATIVE_INFORMATION(203),
	/**
	 * 服务器已经成功处理了请求，但是没有返回内容
	 */
	NO_CONTENT(204),
	/**
	 * 服务器成功，但没有返回内容
	 */
	RESET_CONTENT(205),
	/**
	 * 服务器成功处理了部分请求
	 */
	PARTIAL_CONTENT(206),

	/**
	 * REDIRECTION CODES
	 */
	MULTIPLE_CHOICES(300),
	MOVED_PERMANENTLY(301),
	MOVED_TEMPORARILY(302),
	SEE_OTHER(303),
	NOT_MODIFIED(304),
	USE_PROXY(305),
	REDIRECT_KEEP_VERB(307),

	/**
	 * FAILURE CODES
	 */
	BAD_REQUEST(400),
	UNAUTHORIZED(401),
	PAYMENT_REQUIRED(402),
	FORBIDDEN(403),
	NOT_FOUND(404),
	BAD_REQUEST_METHOD(405),
	NOT_ACCEPTABLE(406),
	PROXY_AUTHENTICATION_REQUIRED(407),
	REQUEST_TIMED_OUT(408),
	CONFILCT(409),
	GONE(410),
	LENGTH_REQUIRED(411),
	PRECONDITION_FAILED(412),
	REQUEST_ENTITY_TOO_LARGE(413),
	REQUEST_URI_TOO_LARGE(414),
	UNSUPPORTED_MEDIA_TYPE(415),

	/**
	 * Server Error Codes
	 */
	INTERNAL_SERVER_ERROR(500),
	NOT_INPLEMENTED(501),
	BAD_GATEWAY(502),
	SERVER_UNAVAILABLE(503),
	GATEWAY_TIMED_OUT(504),
	HTTP_VERSION_NOT_SUPPORTED(505);




	public final int value;

	HttpExceptionCode(int value){
		this.value = value;
	}

}
