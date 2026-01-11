package com.gametout.gametout.dto;

import com.gametout.gametout.enums.BlockType;

public record ContentBlockRequest(
        Integer order,
        BlockType blockType,
        String textContent,
        String mediaUrl,
        String caption
) {}
