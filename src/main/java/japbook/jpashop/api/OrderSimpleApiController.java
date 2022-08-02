package japbook.jpashop.api;

import japbook.jpashop.domain.Order;
import japbook.jpashop.repository.OrderRepository;
import japbook.jpashop.repository.OrderSearch;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * xToOne(ManyToOne, OneToOne)
 * Order
 * Order -> Member
 * Order -> Delivery
 */
@RestController
@RequiredArgsConstructor
public class OrderSimpleApiController {

    private final OrderRepository orderRepository;

    /**
     * 엔티티를 직접 노출한 상태
     * 양방향 연관관계가 걸린 곳은 꼭 한 곳을 @JsonIgnore 처리
     * 즉시로딩은 식별자가 1개일 경우 (join을 해도 식별자가 1개이기 때문에 오히려 성능 최적화 가능)
     * 지연로딩은 jpql일 경우 무조건 지연로딩으로 (실행시 모든 쿼리가 날아가기 때문에 성능 다운됨)
     * @return
     */
    @GetMapping("/api/v1/simple-orders")
    public List<Order> ordersV1() {
        List<Order> all = orderRepository.findAllByString(new OrderSearch());
        for (Order order : all) {
            order.getMember().getName(); //DB에서 데이터를 조회하는 순간 Lazy 강제 초기화 (그 전까진 프록시 객체)
            order.getDelivery().getAddress();
        }
        return all;
    }
}
