package br.com.fiap.lanchonete.core.usecases.pedido;

import br.com.fiap.lanchonete.core.domain.entities.Pedido;
import br.com.fiap.lanchonete.core.domain.enums.StatusEnum;
import br.com.fiap.lanchonete.core.usecases.ports.repositories.PedidoRepositoryPort;
import br.com.fiap.lanchonete.infra.gateways.mp.MercadoPagoRestClient;
import br.com.fiap.lanchonete.infra.gateways.mp.dto.order.CashOut;
import br.com.fiap.lanchonete.infra.gateways.mp.dto.order.Item;
import br.com.fiap.lanchonete.infra.gateways.mp.dto.order.Order;
import br.com.fiap.lanchonete.main.configuration.GatewayPaymentConfiguration;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Objects;

@Service
@Transactional
@RequiredArgsConstructor
public class CheckoutPedidoUsecase {
	
	private static final String DESCRIPTION = "This is the Point Mini";
	private static final String TITLE = "Point Mini";
	private static final String CATEGORY = "marketplace";
	private static final String UNIT = "unit";

	private final PedidoRepositoryPort pedidoPort;
	
	private final MercadoPagoRestClient orderRestClient;

	private final GatewayPaymentConfiguration gatewayPayment;
	
    public Pedido checkoutPedido(Pedido pedido) {
        Pedido ped = pedidoPort.get(pedido.getId());
        
        if (Objects.isNull(ped))
            throw new EntityNotFoundException("Pedido nao encontrado para o id :: " + pedido.getId());

        if (!ped.getStatus().equals(StatusEnum.CRIADO))
            throw new EntityNotFoundException("Pedido já encaminhado nao pode ser mais alterado :: " + pedido.getId());
        
        ped.setCollector(pedido.getCollector());
        ped.setPos(pedido.getPos());
        ped.setStatus(StatusEnum.PENDING);
        
        ArrayList<Item> itens = new ArrayList<>();
		itens.add(buildItem(ped));
		Order order = Order.builder()
				.cashOut(CashOut.builder().amount(new BigDecimal(NumberUtils.INTEGER_ZERO)).build())
				.description(DESCRIPTION).externalReference(pedido.getId().toString()).items(itens)
				.notificationUrl(gatewayPayment.getNotification()).title(TITLE).totalAmount(ped.getPreco()).build();


				System.out.println(" totalammount::: " + ped.getPreco());

		String qrData = orderRestClient.getPaymentQrCode(order, pedido.getCollector(), pedido.getPos(),
				gatewayPayment.getToken());
		ped.setQrData(qrData);
        
        return pedidoPort.checkout(ped);
    }
    
    private Item buildItem(Pedido pedido) {
		return Item.builder().skuNumber(gatewayPayment.getSku()).category(CATEGORY).title(TITLE)
				.description(DESCRIPTION).unitPrice(pedido.getPreco()).quantity(NumberUtils.INTEGER_ONE)
				.unitMeasure(UNIT).totalAmount(pedido.getPreco()).build();
	}

}
