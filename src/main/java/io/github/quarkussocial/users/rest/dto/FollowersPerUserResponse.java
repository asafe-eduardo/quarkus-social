package io.github.quarkussocial.users.rest.dto;

import lombok.Data;

import java.util.List;

@Data
public class FollowersPerUserResponse {
    private Integer followersCount;
    private List<FollowerResponse> content;
}
