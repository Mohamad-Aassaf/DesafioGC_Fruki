package com.fruki.api.config;

import com.fruki.api.dominio.Catalogo;
import com.fruki.api.dominio.Cliente;
import com.fruki.api.dominio.Compra;
import com.fruki.api.dominio.Evento;
import com.fruki.api.dominio.Produto;
import com.fruki.api.repositorio.ClienteRepository;
import com.fruki.api.repositorio.CompraRepository;
import com.fruki.api.repositorio.ConsultaRecomendacaoRepository;
import com.fruki.api.repositorio.EventoRepository;
import com.fruki.api.repositorio.ProdutoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * Carrega a base de demonstração quando o banco está vazio.
 * Os dados são simulados, mas gerados com semente fixa: toda execução produz a mesma base.
 */
@Component
public class DadosIniciais implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DadosIniciais.class);

    static final String RS = "Rep. 33307 (RS)";
    static final String NE = "Rep. 31402 (Nordeste)";
    static final String NO = "Rep. 30110 (Norte)";
    static final String SUL = "Rep. 34120 (SC/PR)";
    static final String SE = "Rep. 35200 (Sudeste e Centro-Oeste)";

    /** Chance de um cliente do segmento comprar cada família (perfil de compra simulado). */
    private static final Map<String, double[]> PERFIL = Map.of(
            //                       PET   lata  energ. água  chás
            "Mercado", new double[]{0.90, 0.60, 0.30, 0.80, 0.50},
            "Supermercado", new double[]{0.95, 0.80, 0.60, 0.90, 0.70},
            "Mercadinho", new double[]{0.80, 0.50, 0.20, 0.60, 0.30},
            "Padaria", new double[]{0.70, 0.50, 0.10, 0.60, 0.70},
            "Bar", new double[]{0.30, 0.90, 0.80, 0.50, 0.10},
            "Atacado", new double[]{0.95, 0.90, 0.70, 0.90, 0.60},
            "Lanchonete", new double[]{0.50, 0.80, 0.40, 0.70, 0.50});

    /** Popularidade dentro da família (1.0 quando não listado); igual aos valores usados em carregar(). */
    private static final Map<String, Double> POPULARIDADE = Map.of(
            "Guaraná Zero 2L", 0.7, "Uva 350 ml", 0.6, "Laranja 2L", 0.7, "Cola 350 ml", 0.9,
            "Elev Zero 473 ml", 0.6, "Água com gás 500 ml", 0.7, "Chá pêssego 1,5 L", 0.8);

    private final ProdutoRepository produtos;
    private final ClienteRepository clientes;
    private final CompraRepository compras;
    private final EventoRepository eventos;
    private final ConsultaRecomendacaoRepository consultas;
    private final JdbcTemplate jdbc;

    public DadosIniciais(ProdutoRepository produtos, ClienteRepository clientes, CompraRepository compras,
                         EventoRepository eventos, ConsultaRecomendacaoRepository consultas, JdbcTemplate jdbc) {
        this.jdbc = jdbc;
        this.produtos = produtos;
        this.clientes = clientes;
        this.compras = compras;
        this.eventos = eventos;
        this.consultas = consultas;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (produtos.count() == 0) {
            carregar();
        } else if (clientes.findAll().stream().noneMatch(c -> c.getNome().equals("Mercado Beira-Mar"))) {
            // Base criada antes da carteira de SC/PR e Sudeste/Centro-Oeste: completa sem apagar nada.
            List<Compra> novas = new ArrayList<>();
            expansao(novas, produtos.findAll());
            compras.saveAll(novas);
            log.info("Clientes de SC/PR, Sudeste e Centro-Oeste adicionados à base existente");
        }
    }

    /** Apaga tudo, volta os ids para 1 e recarrega a base de demonstração (ids iguais aos do README e do Postman). */
    public synchronized void restaurar() {
        consultas.deleteAllInBatch();
        compras.deleteAllInBatch();
        clientes.deleteAllInBatch();
        produtos.deleteAllInBatch();
        eventos.deleteAllInBatch();
        for (String tabela : List.of("consulta_recomendacao", "compra", "cliente", "produto", "evento")) {
            jdbc.execute("ALTER TABLE " + tabela + " ALTER COLUMN id RESTART WITH 1");
        }
        carregar();
    }

    @Transactional
    public void carregar() {
        List<Produto> p = produtos.saveAll(List.of(
                produto("Guaraná 2L", "Refrigerante PET", true, false, 1.0),
                produto("Cola 2L", "Refrigerante PET", true, false, 1.0),
                produto("Guaraná 350 ml", "Refrigerante lata", true, false, 1.0),
                produto("Elev 473 ml", "Energético lata", false, false, 1.0),
                produto("Água sem gás 1,5 L", "Água", true, true, 1.0),
                produto("Chá limão 1,5 L", "Chás e sucos", true, true, 1.0),
                produto("Guaraná Zero 2L", "Refrigerante PET", false, true, 0.7),
                produto("Uva 350 ml", "Refrigerante lata", false, false, 0.6),
                produto("Laranja 2L", "Refrigerante PET", true, false, 0.7),
                produto("Cola 350 ml", "Refrigerante lata", true, false, 0.9),
                produto("Elev Zero 473 ml", "Energético lata", false, false, 0.6),
                produto("Água com gás 500 ml", "Água", true, true, 0.7),
                produto("Chá pêssego 1,5 L", "Chás e sucos", true, true, 0.8)));

        eventos.saveAll(List.of(
                new Evento(proxima(11, 2), "Finados", "BR"),
                new Evento(proxima(11, 20), "Consciência Negra", "BR"),
                new Evento(proxima(11, 27), "Black Friday", "BR", "Refrigerante PET", "Refrigerante lata"),
                new Evento(proxima(12, 25), "Natal", "BR", "Refrigerante PET", "Refrigerante lata"),
                new Evento(proxima(12, 31), "Ano Novo", "BR", "Energético lata", "Refrigerante lata", "Água"),
                new Evento(proxima(2, 2), "Festa de Iemanjá", "S,NE", "Água"),
                new Evento(proxima(2, 9), "Carnaval", "BR", "Água", "Energético lata", "Refrigerante lata"),
                new Evento(proxima(3, 28), "Páscoa", "BR", "Chás e sucos"),
                new Evento(proxima(5, 9), "Dia das Mães", "BR", "Refrigerante PET", "Chás e sucos")));

        Random rnd = new Random(42);
        List<Compra> novas = new ArrayList<>();

        // Clientes do app, com o mesmo mix de produtos (índices 1..8 da lista acima).
        demo(novas, rnd, p, "Mercado Pinheiro", "Canoas", "RS", "Mercado", RS, 1, 2, 5);
        demo(novas, rnd, p, "Padaria Trigo Bom", "Porto Alegre", "RS", "Padaria", RS, 1, 3);
        demo(novas, rnd, p, "Supermercado Lajeado", "Lajeado", "RS", "Supermercado", RS, 1, 2, 3, 4);
        demo(novas, rnd, p, "Minimercado Sol", "Gravataí", "RS", "Mercadinho", RS, 2);
        demo(novas, rnd, p, "Mercadinho Central", "Salvador", "BA", "Mercadinho", NE, 1, 3);
        demo(novas, rnd, p, "Atacado Recife Sul", "Recife", "PE", "Atacado", NE, 1, 2, 4);
        demo(novas, rnd, p, "Bar da Praça", "Fortaleza", "CE", "Bar", NE, 3);
        demo(novas, rnd, p, "Mercado Rio Negro", "Manaus", "AM", "Mercado", NO, 1, 2);
        demo(novas, rnd, p, "Distribuidora Belém", "Belém", "PA", "Atacado", NO, 1, 3, 5);

        // Base histórica: compras sorteadas pelo perfil do segmento.
        perfil(novas, rnd, p, "Padaria Pão Quente", "Novo Hamburgo", "RS", "Padaria", RS);
        perfil(novas, rnd, p, "Padaria Santa Clara", "Caxias do Sul", "RS", "Padaria", RS);
        perfil(novas, rnd, p, "Bar do Gaúcho", "Porto Alegre", "RS", "Bar", RS);
        perfil(novas, rnd, p, "Supermercado Serrano", "Bento Gonçalves", "RS", "Supermercado", RS);
        perfil(novas, rnd, p, "Mercado Bom Fim", "Porto Alegre", "RS", "Mercado", RS);
        perfil(novas, rnd, p, "Mercado Vale Verde", "Santa Cruz do Sul", "RS", "Mercado", RS);
        perfil(novas, rnd, p, "Minimercado Estrela", "Pelotas", "RS", "Mercadinho", RS);
        perfil(novas, rnd, p, "Mercadinho Boa Vista", "Natal", "RN", "Mercadinho", NE);
        perfil(novas, rnd, p, "Bar Maré Alta", "Maceió", "AL", "Bar", NE);
        perfil(novas, rnd, p, "Bar do Forró", "Recife", "PE", "Bar", NE);
        perfil(novas, rnd, p, "Padaria Sertão", "Fortaleza", "CE", "Padaria", NE);
        perfil(novas, rnd, p, "Atacado Nordestão", "Salvador", "BA", "Atacado", NE);
        perfil(novas, rnd, p, "Mercado São João", "João Pessoa", "PB", "Mercado", NE);
        perfil(novas, rnd, p, "Mercado Ver-o-Peso", "Belém", "PA", "Mercado", NO);
        perfil(novas, rnd, p, "Atacado Amazonas", "Manaus", "AM", "Atacado", NO);
        perfil(novas, rnd, p, "Supermercado Tapajós", "Santarém", "PA", "Supermercado", NO);
        perfil(novas, rnd, p, "Bar Encontro das Águas", "Manaus", "AM", "Bar", NO);
        perfil(novas, rnd, p, "Padaria Pará", "Belém", "PA", "Padaria", NO);

        // Cliente novo, sem nenhuma compra: demonstra o caso "cliente sem histórico".
        clientes.save(new Cliente("Lanchonete Nova Era", "Canoas", "RS", "Lanchonete", RS));

        // Carteira menor fora do RS (ids 29 em diante, para não mudar os ids já documentados).
        expansao(novas, p);

        compras.saveAll(novas);
        log.info("Base de demonstração carregada: {} produtos, {} clientes, {} compras",
                p.size(), clientes.count(), novas.size());
    }

    /** Clientes de Santa Catarina, Paraná, Sudeste e Centro-Oeste, com semente própria. */
    private void expansao(List<Compra> novas, List<Produto> p) {
        if (fator.isEmpty()) {
            for (Produto prod : p) fator.put(prod.getNome(), POPULARIDADE.getOrDefault(prod.getNome(), 1.0));
        }
        Random rnd = new Random(7);
        perfil(novas, rnd, p, "Mercado Beira-Mar", "Florianópolis", "SC", "Mercado", SUL);
        perfil(novas, rnd, p, "Padaria Vale Europeu", "Blumenau", "SC", "Padaria", SUL);
        perfil(novas, rnd, p, "Supermercado Araucária", "Curitiba", "PR", "Supermercado", SUL);
        perfil(novas, rnd, p, "Bar Pé Vermelho", "Londrina", "PR", "Bar", SUL);
        perfil(novas, rnd, p, "Mercado Paulista", "Campinas", "SP", "Mercado", SE);
        perfil(novas, rnd, p, "Padaria Mineira", "Belo Horizonte", "MG", "Padaria", SE);
        perfil(novas, rnd, p, "Atacado Cerrado", "Goiânia", "GO", "Atacado", SE);
    }

    /** Próxima ocorrência da data (este ano, ou o próximo se já passou): os eventos ficam sempre à frente. */
    private static LocalDate proxima(int mes, int dia) {
        LocalDate hoje = LocalDate.now();
        LocalDate d = LocalDate.of(hoje.getYear(), mes, dia);
        return d.isBefore(hoje) ? d.plusYears(1) : d;
    }

    /** Fator de popularidade do produto dentro da família, guardado só durante a carga. */
    private final Map<String, Double> fator = new java.util.HashMap<>();

    private Produto produto(String nome, String familia, boolean quente, boolean saudavel, double popularidade) {
        fator.put(nome, popularidade);
        BigDecimal preco = BigDecimal.valueOf(20 + Catalogo.hash(nome) % 40).setScale(2);
        return new Produto(nome, familia, preco, quente, saudavel);
    }

    private void demo(List<Compra> novas, Random rnd, List<Produto> p, String nome, String cidade, String uf,
                      String segmento, String vendedor, int... mix) {
        Cliente c = clientes.save(new Cliente(nome, cidade, uf, segmento, vendedor));
        for (int i : mix) comprar(novas, rnd, c, p.get(i - 1));
    }

    private void perfil(List<Compra> novas, Random rnd, List<Produto> p, String nome, String cidade, String uf,
                        String segmento, String vendedor) {
        Cliente c = clientes.save(new Cliente(nome, cidade, uf, segmento, vendedor));
        double[] chance = PERFIL.get(segmento);
        for (Produto prod : p) {
            double prob = chance[Catalogo.FAMILIAS.indexOf(prod.getFamilia())] * fator.getOrDefault(prod.getNome(), 1.0);
            if (rnd.nextDouble() < prob) comprar(novas, rnd, c, prod);
        }
    }

    /** De 1 a 4 pedidos do produto nos últimos 120 dias, de 5 a 60 caixas cada. */
    private static void comprar(List<Compra> novas, Random rnd, Cliente c, Produto p) {
        LocalDate hoje = LocalDate.now();
        Set<Integer> diasUsados = new HashSet<>();
        int pedidos = 1 + rnd.nextInt(4);
        for (int i = 0; i < pedidos; i++) {
            int d = 1 + rnd.nextInt(120);
            if (diasUsados.add(d)) novas.add(new Compra(c, p, 5 + rnd.nextInt(56), hoje.minusDays(d)));
        }
    }
}
