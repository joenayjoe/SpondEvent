package com.junaid.spond.exceptions;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class ErrorResponse {
  private String title;
  private int status;
  private String message;
  private String httpMethod;
  private String path;
  private LocalDateTime occurredAt;

  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  @Builder.Default
  private Map<String, List<ValidationError>> errors = new HashMap<>();
}
