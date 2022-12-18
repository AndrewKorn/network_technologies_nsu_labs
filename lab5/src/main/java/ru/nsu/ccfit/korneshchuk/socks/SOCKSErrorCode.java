package ru.nsu.ccfit.korneshchuk.socks;

public enum SOCKSErrorCode {
    SUCCESS(0x00),
    GENERAL_SOCKS_SERVER_FAILURE(0x01),
    CONNECTION_NOT_ALLOWED_BY_RULE_SET_DEFINED_AT_SERVER(0x02),
    DESTINATION_NETWORK_UNREACHABLE(0x03),
    DESTINATION_HOST_UNREACHABLE(0x04),
    CONNECTION_REFUSED_BY_REMOTE_HOST(0x05),
    TTL_EXPIRED(0x06),
    COMMAND_NOT_SUPPORTED(0x07),
    ADDRESS_TYPE_NOT_SUPPORTED(0x08),
    REQUEST_REJECTED_OR_FAILED((byte) 0x91),
    REQUEST_REJECTED_BECAUSE_SERVER_COULD_NOT_CONTACT_IDENT_SERVER((byte) 0x92),
    REQUEST_REJECTED_BECAUSE_CLIENT_PROGRAM_AND_IDENT_REPORTED_DIFFERENT_USER_IDENTITIES((byte) 0x93);

    private final int errorCode;

    SOCKSErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public int getCode() {
        return errorCode;
    }

    public byte getCodeAsByte() {
        if (errorCode >= Byte.MAX_VALUE) {
            throw new IllegalArgumentException("Error code more than 1 byte, actual=" + errorCode);
        }
        return (byte) errorCode;
    }
}