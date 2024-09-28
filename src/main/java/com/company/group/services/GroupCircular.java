package com.company.group.services;

import com.company.group.components.GroupEntity;

import java.util.List;

public interface GroupCircular {

    boolean isExist(Long chatId);

    List<GroupEntity> getAll();

    boolean checkKeyWord(String text);
}
