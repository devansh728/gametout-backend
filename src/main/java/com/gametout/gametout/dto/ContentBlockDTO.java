package com.gametout.gametout.dto;

import com.gametout.gametout.enums.BlockType;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ContentBlockDTO {
    private Long id;
    private Integer blockOrder;
    private BlockType blockType;
    private String textContent;
    private String mediaUrl;

    private String caption;
}
