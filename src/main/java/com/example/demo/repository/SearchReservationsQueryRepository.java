package com.example.demo.repository;

import com.example.demo.entity.QItem;
import com.example.demo.entity.QReservation;
import com.example.demo.entity.QUser;
import com.example.demo.entity.Reservation;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

//querydsl을 사용하는 레포지토리
@Repository
@RequiredArgsConstructor
public class SearchReservationsQueryRepository {

    private final JPAQueryFactory queryFactory;

    private static final QReservation reservation = QReservation.reservation;
    private static final QUser user = QUser.user;
    private static final QItem item = QItem.item;

    public List<Reservation> searchReservations(Long userId, Long itemId) {
        return queryFactory.selectFrom(reservation)
            .join(reservation.user, user).fetchJoin()
            .join(reservation.item, item).fetchJoin()
            .where(Expressions.allOf(
                validationOfUserIdConditions(userId), validationOfItemIdConditions(itemId)
            ))
            .fetch();
    }

    private BooleanExpression validationOfUserIdConditions(Long userId){
        if (userId == null){
            return null;
        }

        return reservation.user.id.eq(userId);
    }

    private BooleanExpression validationOfItemIdConditions(Long itemId){
        if (itemId == null){
            return null;
        }

        return reservation.item.id.eq(itemId);
    }
}
