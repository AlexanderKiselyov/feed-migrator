package polis.domain;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.util.StdConverter;

public abstract class Media {

    @JsonSerialize(converter = TypeConverter.class)
    public final Type type;

    public Media(Type type) {
        this.type = type;
    }

    public enum Type {
        PHOTO("photo");

        public final String jsonValue;

        Type(String jsonValue) {
            this.jsonValue = jsonValue;
        }

    }

    private static class TypeConverter extends StdConverter<Type, String> {
        @Override
        public String convert(Type value) {
            return value.jsonValue;
        }
    }
}
