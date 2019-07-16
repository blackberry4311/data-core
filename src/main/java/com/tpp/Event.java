package com.tpp;


/**
 * Event is an object with name as identity and data which can be an Object
 * or Object[] array
 */
public class Event {
    String name;
    Object data;

    public Event(String name, Object data) {
        if (name == null || data == null)
            throw new IllegalArgumentException("name or data is missing");
        this.name = name;
        this.data = data;
    }

    public String getName() {
        return name;
    }

    public Object getData() {
        return data;
    }

    @Override
    public String toString() {
        return name + " " + data;
    }

    public <T> T getParam(int index) {
        if (data != null && data.getClass().isArray()) {
            Object[] params = (Object[]) data;
            if (index > params.length) throw new IndexOutOfBoundsException("event only has "
                    + params.length + " parameters in data");
            @SuppressWarnings("unchecked")
            T value = (T) params[index];
            return value;
        } else {
            if (index > 0) throw new IndexOutOfBoundsException("event only has one parameters in data");
            @SuppressWarnings("unchecked")
            T value = (T) data;
            return value;
        }
    }
}
