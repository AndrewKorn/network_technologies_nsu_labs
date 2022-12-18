package ru.nsu.ccfit.korneshchuk.attachment;

public enum ClientHandlerState {
    CLIENT_AUTHENTICATION_WAITING,
    SEND_CLIENT_AUTHENTICATION,
    CLIENT_REQUEST_WAITING,
    SEND_CLIENT_RESPONSE,
    FORWARDING,
    SEND_CLIENT_ERROR
}