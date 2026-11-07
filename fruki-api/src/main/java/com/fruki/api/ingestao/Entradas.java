package com.fruki.api.ingestao;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** Formatos de entrada (JSON) da ingestão. As regras simples ficam nas anotações; as de negócio, no serviço. */
public final class Entradas {
    private Entradas() {}

    public record ClienteEntrada(
            @NotBlank(message = "nome é obrigatório") @Size(max = 120, message = "nome: até 120 caracteres") String nome,
            @NotBlank(message = "cidade é obrigatória") @Size(max = 80, message = "cidade: até 80 caracteres") String cidade,
            @NotBlank(message = "uf é obrigatória") String uf,
            @NotBlank(message = "segmento é obrigatório") String segmento,
            @Size(max = 60, message = "vendedor: até 60 caracteres") String vendedor) {}

    public record ProdutoEntrada(
            @NotBlank(message = "nome é obrigatório") @Size(max = 80, message = "nome: até 80 caracteres") String nome,
            @NotBlank(message = "familia é obrigatória") String familia,
            @NotNull(message = "preco é obrigatório") @Positive(message = "preco deve ser maior que zero")
            @DecimalMax(value = "1000", message = "preco deve ser no máximo R$ 1000 por caixa") BigDecimal preco,
            Boolean quente,
            Boolean saudavel) {}

    public record CompraEntrada(
            @NotNull(message = "clienteId é obrigatório") Long clienteId,
            @NotNull(message = "produtoId é obrigatório") Long produtoId,
            @NotNull(message = "quantidade é obrigatória") @Min(value = 1, message = "quantidade deve ser pelo menos 1")
            @Max(value = 10000, message = "quantidade deve ser no máximo 10000") Integer quantidade,
            /** Opcional: sem data, vale hoje. */
            LocalDate data) {}

    /** Carga em lote: cada item é validado sozinho; os válidos entram, os inválidos voltam no relatório. */
    public record LoteEntrada(List<ClienteEntrada> clientes, List<ProdutoEntrada> produtos, List<CompraEntrada> compras) {}
}
