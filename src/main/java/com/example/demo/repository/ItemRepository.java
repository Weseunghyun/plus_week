package com.example.demo.repository;

import com.example.demo.entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {

    default Item findByIdOrElseThrow(Long id) {
        return findById(id).orElseThrow(
            () -> new IllegalStateException("존재하지않는 아이템입니다.")
        );
    }
}
