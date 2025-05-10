package com.junaid.spond.dtos;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class PageableResponse<T> {
  private List<T> data;
  private int currentPage;
  private int totalPages;
  private long totalItems;
}
