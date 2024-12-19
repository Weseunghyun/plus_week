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
    @DisplayName("통합 테스트 활용해서 nullable=false 확인하기")
     void statusNullableCheck() {
        User owner = new User();
        User manager = new User();
        Item item = new Item("아이템", "설명", owner, manager);
        item.setStatus(null);

        //nullable = false 를 확인하려면 item Entity의 @DynamicInsert 를 제거해줘야함.
        assertThrows(DataIntegrityViolationException.class,
            () -> itemRepository.saveAndFlush(item)
        );
    }
}