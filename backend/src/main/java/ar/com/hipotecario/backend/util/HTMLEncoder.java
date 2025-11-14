package ar.com.hipotecario.backend.util;

import com.github.jknack.handlebars.internal.text.StringEscapeUtils;

public class HTMLEncoder {

	public static String decodeHTML(String html) {
		return StringEscapeUtils.unescapeHtml4(html);
	}
}
