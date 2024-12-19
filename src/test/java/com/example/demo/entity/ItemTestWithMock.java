package com.example.demo.entity;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.example.demo.repository.ItemRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

@ExtendWith(MockitoExtension.class)
class ItemTestWithMock {

    @Mock
    private ItemRepository itemRepository;

    @Test
    void statusNullableCheck() {
        //given
        User owner = new User();
        User manager = new User();
        Item item = new Item("아이템", "설명", owner, manager);
        item.setStatus(null);

        when(itemRepository.save(item)).thenThrow(DataIntegrityViolationException.class);

        //when, then
        assertThrows(DataIntegrityViolationException.class, () -> itemRepository.save(item));

    }
}
