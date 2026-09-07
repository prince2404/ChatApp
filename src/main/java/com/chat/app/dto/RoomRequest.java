package com.chat.app.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomRequest {

    @NotBlank(message = "Room ID is required")
    @Size(min = 2, max = 30, message = "Room ID must be between 2 and 30 characters")
    @Pattern(regexp = "^[a-zA-Z0-9_-]+$", message = "Room ID can only contain alphanumeric characters, underscores, or hyphens")
    private String roomId;

    @Size(max = 50, message = "Room name must be under 50 characters")
    private String name;

    @Size(max = 150, message = "Room description must be under 150 characters")
    private String description;
}
