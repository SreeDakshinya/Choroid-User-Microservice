package com.ddbs.choroid_user_service.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SearchQueryUser {
    private String nameSubstring;
    private List<String> teachList;
    private List<String> learnList;
}
