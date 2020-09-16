import jade.core.Agent;

public class start {
    public static void main(String[] args) {

        // Inicia a G.U.I. para o JADE
        String[] parametros = {
                "-gui",
                "-local-host",
                "127.0.0.1"
        };
        jade.Boot.main(parametros);

        // Iniciando o container
        String[] novoContainer = {
                "-local-host", "127.0.0.1",
                "-container", "-container-name",
                "Container-Almeida"};
        jade.Boot.main(novoContainer);
    }

    public class AgenteComprador extends Agent {
    }

    public class AgenteVendedor extends Agent {
    }


}
