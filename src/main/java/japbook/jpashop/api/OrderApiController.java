package japbook.jpashop.api;

import japbook.jpashop.domain.Address;
import japbook.jpashop.domain.Order;
import japbook.jpashop.domain.OrderItem;
import japbook.jpashop.domain.OrderStatus;
import japbook.jpashop.repository.OrderRepository;
import japbook.jpashop.repository.OrderSearch;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class OrderApiController {

    private final OrderRepository orderRepository;

    @GetMapping("/api/v1/orders")
    public List<Order> orderV1() {
        List<Order> all = orderRepository.findAllByString(new OrderSearch());
        for (Order order : all) {
            order.getMember().getName();
            order.getDelivery().getAddress();
            List<OrderItem> orderItems = order.getOrderItems();
            orderItems.stream().forEach(o -> o.getItem().getName());
        }
        return all;
    }

    @GetMapping("/api/v2/orders")
    public List<OrderDto> ordersV2() {
        List<Order> orders = orderRepository.findAllByString(new OrderSearch());
        List<OrderDto> result = orders.stream()
                .map(o -> new OrderDto(o))
                .collect(Collectors.toList());
        return result;
    }

    /**
     * 패치 조인으로 최적화
     */
    @GetMapping("/api/v3/orders")
    public List<OrderDto> ordersV3() {
        List<Order> orders = orderRepository.findAllWithItem();
        List<OrderDto> result = orders.stream()
                .map(o -> new OrderDto(o))
                .collect(Collectors.toList());
        return result;
    }

    /**
     * 패치 조인_페이징 처리하기
     * Orderitems과 item에: DTO에서 값을 넣을 때 프록시가 초기화되면서 쿼리가 나간다.
     * (V3는 쿼리문에 Orderitems, item을 패치조인하여 한방 쿼리로 가져옴)
     * application.yml에서 default_batch_fetch_size: 100추가
     * (detail하게 사용하고 싶으면 엔티티에 @BatchSize(size=100)으로 적어주면 된다. 일대다 경우 해당 필드에, 다대일경우 @Entity 선언한 위치에)
     * → 이렇게 되면 Orderitems, item에 대한 값을 미리 다 가져와버린다. (프록시 초기화시 쿼리가 나갈 때)
     * @return
     */
    @GetMapping("/api/v3.1/orders")
    public List<OrderDto> ordersV3_page(
            @RequestParam(value = "offset", defaultValue = "0") int offset,
            @RequestParam(value = "limit", defaultValue = "100") int limit) {
        List<Order> orders = orderRepository.findAllWithMemberDelivery(offset, limit);



        List<OrderDto> result = orders.stream()
                .map(o -> new OrderDto(o))
                .collect(Collectors.toList());
        return result;
    }

    @Getter
    static class OrderDto {

        private Long orderId;
        private String name;
        private LocalDateTime orderDate;
        private OrderStatus orderStatus;
        private Address address;
        private List<OrderItemDto> orderItems;
        public OrderDto(Order order) {
            orderId = order.getId();
            name = order.getMember().getName();//프록시 초기화되는 시점(지연로딩)
            orderDate = order.getOrderDate();
            address = order.getDelivery().getAddress(); //프록시 초기화되는 시점(지연로딩)
            orderItems = order.getOrderItems().stream()
                    .map(o -> new OrderItemDto(o))
                    .collect(Collectors.toList());

        }
    }

    @Getter
    static class OrderItemDto {

        private String itmeName; //상품명
        private int orderPrice; //주문 가격
        private int count; //주문 수량
        public OrderItemDto(OrderItem orderItem) {
            itmeName = orderItem.getItem().getName();//프록시 초기화되는 시점(지연로딩)
            orderPrice = orderItem.getOrderPrice();
            count = orderItem.getCount();
        }
    }
}
