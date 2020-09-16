import jade.core.Agent;

public class start {

    public class AgenteComprador extends Agent {
    }

    public class AgenteVendedor extends Agent {
    }
    
    public static void main(String[] args) {

        // Inicia a G.U.I. para o JADE
        String[] parametros = {
                "-gui",
                "-local-host",
                "127.0.0.1",
                "comprador:AgenteComprador()",
        };
        jade.Boot.main(parametros);

        // Iniciando o container
        String[] novoContainer = {
                "-local-host", "127.0.0.1",
                "-container", "-container-name",
                "Container-Almeida", "vendedor:AgenteVendedor()"};
        jade.Boot.main(novoContainer);
    }

}
