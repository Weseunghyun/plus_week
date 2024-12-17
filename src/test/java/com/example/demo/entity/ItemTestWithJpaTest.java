package com.example.demo.entity;

import static org.junit.jupiter.api.Assertions.assertThrows;

import com.example.demo.repository.ItemRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ItemTestWithJpaTest {

    @Autowired
    private ItemRepository itemRepository;

    @Test
    @DisplayName("DataJpaTest 활용해서 nullable=false 확인하기")
    void checkStatusNullableTest(){
        User owner = new User();
        User manager = new User();
        Item item = new Item("아이템", "설명", owner, manager);

        //nullable = false 를 확인하려면 item Entity의 @DynamicInsert 를 제거해줘야함.
        assertThrows(DataIntegrityViolationException.class, () -> itemRepository.save(item));
    }
}
