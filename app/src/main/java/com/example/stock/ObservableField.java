package com.example.stock;

public class ObservableField<T> {
    private T value;
    private OnChangeListener<T> listener;

    public interface OnChangeListener<T> {
        void onChange(T oldValue, T newValue);
    }

    public void setValue(T newValue) {
        T oldValue = this.value;
        this.value = newValue;
        if (listener != null && !newValue.equals(oldValue)) {
            listener.onChange(oldValue, newValue);
        }
    }

    public T getValue() {
        return value;
    }

    public void setOnChangeListener(OnChangeListener<T> listener) {
        this.listener = listener;
    }
}
