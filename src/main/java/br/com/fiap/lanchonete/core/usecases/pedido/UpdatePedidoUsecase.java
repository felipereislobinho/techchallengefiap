package br.com.fiap.lanchonete.core.usecases.pedido;

import br.com.fiap.lanchonete.core.domain.entities.Pedido;
import br.com.fiap.lanchonete.core.usecases.ports.repositories.PedidoRepositoryPort;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import java.util.Objects;

import org.springframework.stereotype.Service;

@Service
@Transactional
@RequiredArgsConstructor
public class UpdatePedidoUsecase {

	private final PedidoRepositoryPort pedidoPort;

	public Pedido update(Long id, Pedido pedido) {
		if (Objects.nonNull(id)) {
			pedido.setId(id);
			Pedido pedido1 = pedidoPort.get(id);
			if (Objects.isNull(pedido1))
				return null;
			pedido1.setStatus(pedido.getStatus());
			return pedidoPort.save(pedido1);
		}
		return null;
	}

}
