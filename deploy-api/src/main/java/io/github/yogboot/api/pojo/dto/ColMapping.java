package io.github.yogboot.api.pojo.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ColMapping {

    private String fromCol;

    private String toCol;
}
