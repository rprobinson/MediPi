package com.dev.ops.common.utils;

import java.io.IOException;
import java.io.StringWriter;
import java.util.List;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.MappingJsonFactory;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.type.TypeFactory;

import com.dev.ops.exceptions.impl.WrappedSystemException;

public final class JSONUtil {

	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	private static final MappingJsonFactory MAPPING_JSON_FACTORY = new MappingJsonFactory();

	private JSONUtil() {
	}

	public static <T> Object fromJSON(final String jsonString, final Class<T> clazz) throws IOException {
		return OBJECT_MAPPER.readValue(jsonString, clazz);
	}

	public static <T> Object fromObject(final Object object, final Class<T> clazz) throws IOException {
		return fromJSON(createJSON(object), clazz);
	}

	public static <T> List<T> fromJSONToList(final String jsonString, final Class<T> clazz) throws IOException {
		final TypeFactory typeFactory = TypeFactory.defaultInstance();
		return OBJECT_MAPPER.readValue(jsonString, typeFactory.constructCollectionType(List.class, clazz));
	}

	public static String createJSON(final Object object) throws IOException {
		final StringWriter stringWriter = new StringWriter();
		final JsonGenerator jsonGenerator = MAPPING_JSON_FACTORY.createJsonGenerator(stringWriter);
		OBJECT_MAPPER.writeValue(jsonGenerator, object);
		stringWriter.close();
		return stringWriter.getBuffer().toString();
	}

	public static String getAttributeValueFromJSON(final String jsonString, final String attributeKey) {
		final int attributeKeyBeginIndex = jsonString.indexOf(attributeKey);

		if(attributeKeyBeginIndex < 0) {
			return null;
		}

		final int attrbutValIdentifierIndex = attributeKeyBeginIndex + attributeKey.length();
		String attributeVal = jsonString.substring(attrbutValIdentifierIndex + 1, jsonString.indexOf(',', attrbutValIdentifierIndex));
		attributeVal = attributeVal.replace("\"", "").replace("}", "");
		return attributeVal.trim();
	}

	public static <T> Object fromJsonUnchecked(final String jsonAsString, final Class<T> pojoClass) {
		try {
			return OBJECT_MAPPER.readValue(jsonAsString, pojoClass);
		} catch(final Exception e) {
			throw new WrappedSystemException("Error in fromJsonUnchecked() for class=" + pojoClass.getName() + " and source=" + jsonAsString, e);
		}
	}
}