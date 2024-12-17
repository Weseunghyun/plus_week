package com.example.demo.entity;

import static org.junit.jupiter.api.Assertions.*;

import com.example.demo.repository.ItemRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;

@SpringBootTest
class ItemTest {

    @Autowired
    private ItemRepository itemRepository; // 주입

    @Test
    @DisplayName("status nullable = false 적용되는지 확인!!!")
     void statusNullableCheck() {
        User owner = new User();
        User manager = new User();
        Item item = new Item("아이템", "설명", owner, manager);
        item.setStatus(null);

        assertThrows(DataIntegrityViolationException.class,
            () -> itemRepository.saveAndFlush(item)
        );
    }
}