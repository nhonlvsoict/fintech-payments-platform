package nhonlv.soict.banking_platform_service.payments.api.dto;

public record ApiResponse<T>(T data, ApiStatusResponse error) {

    //for 200, 201 response
  public static <T> ApiResponse<T> success(T data) {
    return new ApiResponse<>(data, null);
  }

  //for 202 response
  public static <T> ApiResponse<T> pending(String code, String message) {
    return new ApiResponse<>(null, new ApiStatusResponse(code, message));
  }

  //for 400, 404, 409 response
  public static <T> ApiResponse<T> failure(String code, String message) {
    return new ApiResponse<>(null, new ApiStatusResponse(code, message));
  }
}
