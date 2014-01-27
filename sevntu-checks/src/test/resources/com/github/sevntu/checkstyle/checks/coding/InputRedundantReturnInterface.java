package com.revere.ld.auth;

import org.apache.commons.httpclient.methods.PostMethod;

public interface AuthHttpRequestDecorator {

	void decorate(PostMethod post);

}
