package com.Satisfyre.app.dto;

import com.Satisfyre.app.entity.UserEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DownlineDTO {
    private UserEntity user;
    private int level; // 1 = direct downline, 2 = downline of downline, etc.
}
