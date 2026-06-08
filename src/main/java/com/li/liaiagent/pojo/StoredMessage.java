package com.li.liaiagent.pojo;

import lombok.Data;

@Data
public class StoredMessage {
    private String type;    // system / user / assistant
    private String content;
}