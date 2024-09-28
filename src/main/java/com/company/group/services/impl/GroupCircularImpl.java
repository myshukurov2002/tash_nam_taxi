package com.company.group.services.impl;

import com.company.group.components.GroupEntity;
import com.company.group.GroupRepository;
import com.company.group.services.GroupCircular;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GroupCircularImpl implements GroupCircular {

    String KEY_WORDS = "bor kk kerak kirag kerag бор кк керак кираг кераг";

    @Value("${taxi.group.id}")
    private Long TAXI_GROUP_ID;

    private final GroupRepository groupRepository;


    @Override
    @Transactional
    public boolean isExist(Long chatId) {
        if (!groupRepository.existsById(chatId)) {
            groupRepository.save(new GroupEntity(chatId));
            return true;
        }
        return false;
    }

    @Override
    public List<GroupEntity> getAll() {
        return groupRepository
                .findAll()
                .stream()
                .filter(g -> g.getGroupId() != TAXI_GROUP_ID)
                .toList();
    }

    @Override
    public boolean checkKeyWord(String text) {
        //TODO
        return KEY_WORDS.contains(text);
    }
}
