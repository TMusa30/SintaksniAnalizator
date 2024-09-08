import java.util.*;

public class SintaksniAnalizator {


    public static void main(String[] args) {

        List<String> zavrsniZnakovi = List.of("IDN", "BROJ", "KR_ZA", "KR_DO", "KR_OD", "KR_AZ", "OP_PRIDRUZI",
                "OP_PLUS", "OP_MINUS", "OP_PUTA", "OP_DIJELI", "L_ZAGRADA", "D_ZAGRADA", "$");

        List<String> nezavrsniZnakovi = List.of("PROGRAM", "LN", "N", "NP", "ZP", "E", "EL", "T", "TL", "P");

        Map<String, String> puniNaziviNeZavrsnihZnakova = Map.of(
                "PROGRAM", "program",
                "LN", "lista_naredbi",
                "N", "naredba",
                "NP", "naredba_pridruzivanja",
                "ZP", "za_petlja",
                "E", "E",
                "EL", "E_lista",
                "T", "T",
                "TL", "T_lista",
                "P", "P"
        );


        Map<String, Map<String, List<String>>> prijelazi = Map.of(
                "PROGRAM", Map.of(
                        "IDN", List.of("LN", "SD"),
                        "KR_ZA", List.of("LN", "SD"),
                        "$", List.of("LN", "SD")
                ),
                "LN", Map.of(
                        "IDN", List.of("N", "LN", "SD"),
                        "KR_ZA", List.of("N", "LN", "SD"),
                        "KR_AZ", List.of("$", "SD"),
                        "$", List.of("$", "SD")
                ),
                "N", Map.of(
                        "IDN", List.of("NP", "SD"),
                        "KR_ZA", List.of("ZP", "SD")
                ),
                "NP", Map.of(
                        "IDN", List.of("IDN", "OP_PRIDRUZI", "E", "SD")
                ),
                "ZP", Map.of(
                        "KR_ZA", List.of("KR_ZA", "IDN", "KR_OD", "E", "KR_DO", "E", "LN", "KR_AZ", "SD")
                ),
                "E", Map.of(
                        "IDN", List.of("T", "EL", "SD"),
                        "BROJ", List.of("T", "EL", "SD"),
                        "OP_PLUS", List.of("T", "EL", "SD"),
                        "OP_MINUS", List.of("T", "EL", "SD"),
                        "L_ZAGRADA", List.of("T", "EL", "SD")
                ),
                "EL", Map.of(
                        "OP_PLUS", List.of("OP_PLUS", "E", "SD"),
                        "OP_MINUS", List.of("OP_MINUS", "E", "SD"),
                        "IDN", List.of("$", "SD"),
                        "KR_ZA", List.of("$", "SD"),
                        "KR_DO", List.of("$", "SD"),
                        "KR_AZ", List.of("$", "SD"),
                        "D_ZAGRADA", List.of("$", "SD"),
                        "$", List.of("$", "SD")
                ),
                "T", Map.of(
                        "IDN", List.of("P", "TL", "SD"),
                        "BROJ", List.of("P", "TL", "SD"),
                        "OP_PLUS", List.of("P", "TL", "SD"),
                        "OP_MINUS", List.of("P", "TL", "SD"),
                        "L_ZAGRADA", List.of("P", "TL", "SD")
                ),
                "TL", Map.of(
                        "OP_PUTA", List.of("OP_PUTA", "T", "SD"),
                        "OP_DIJELI", List.of("OP_DIJELI", "T", "SD"),
                        "IDN", List.of("$", "SD"),
                        "KR_ZA", List.of("$", "SD"),
                        "KR_DO", List.of("$", "SD"),
                        "KR_AZ", List.of("$", "SD"),
                        "D_ZAGRADA", List.of("$", "SD"),
                        "$", List.of("$", "SD"),
                        "OP_PLUS", List.of("$", "SD"),
                        "OP_MINUS", List.of("$", "SD")
                ),
                "P", Map.of(
                        "IDN", List.of("IDN", "SD"),
                        "BROJ", List.of("BROJ", "SD"),
                        "L_ZAGRADA", List.of("L_ZAGRADA", "E", "D_ZAGRADA", "SD"),
                        "OP_PLUS", List.of("OP_PLUS", "P", "SD"),
                        "OP_MINUS", List.of("OP_MINUS", "P", "SD")
                )
        );


        Stack<String> stog = new Stack<>();
        List<Pair<String, String>> ulaz = new ArrayList<>();
        StringBuilder stablo = new StringBuilder();

        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNextLine()) {
            String scan = scanner.nextLine();
            String[] scanSplit = scan.split(" ");
            ulaz.add(new Pair<>(scanSplit[0], scan));
        }

        stog.push("PROGRAM");
        int dubina = 0;

        for (int i = 0; i <= ulaz.size(); i++) {
            if (i == ulaz.size() || stog.isEmpty()) {
                obradiStogDoKraja(stog, dubina, stablo, nezavrsniZnakovi, zavrsniZnakovi, puniNaziviNeZavrsnihZnakova);
            }

            Pair<String, String> ulazniZnak = ulaz.get(i);
            String znakNaUlazu = ulazniZnak.first;
            String linija = ulazniZnak.second;

            if (stog.peek().equals("SD")) {
                stog.pop();
                i--;
                dubina--;
                continue;
            }

            if (stog.peek().equals("$")) {
                stablo.append(" ".repeat(dubina)).append("$\n");
                i--;
                stog.pop();
                continue;
            }

            if (nezavrsniZnakovi.contains(stog.peek())) {
                i--;
                Map<String, List<String>> prijelaziZaVrhStoga = prijelazi.get(stog.peek());
                List<String> prijelaz = prijelaziZaVrhStoga.get(znakNaUlazu);
                if (prijelaz == null) {
                    System.out.println("err " + linija);
                    System.exit(0);
                }

                stablo.append(" ".repeat(dubina++)).append("<").append(puniNaziviNeZavrsnihZnakova.get(stog.peek())).append(">\n");
                stog.pop();
                for (int j = prijelaz.size() - 1; j >= 0; j--) {
                    stog.push(prijelaz.get(j));
                }
            } else if (zavrsniZnakovi.contains(stog.peek())) {
                if (stog.peek().equals(znakNaUlazu)) {
                    stablo.append(" ".repeat(dubina)).append(linija).append("\n");
                    stog.pop();
                } else {
                    System.out.println("err " + linija);
                    System.exit(0);
                }
            } else {
                System.out.println("err " + linija);
                System.exit(0);
            }
        }
    }

    private static void obradiStogDoKraja(Stack<String> stog, int dubina, StringBuilder stablo, List<String> nezavrsniZnakovi, List<String> zavrsniZnakovi,
                                          Map<String, String> puniNaziviNeZavrsnihZnakova) {
        while (!stog.isEmpty()) {
            if (stog.peek().equals("SD")) {
                stog.pop();
                dubina--;
                continue;
            }
            if (nezavrsniZnakovi.contains(stog.peek())) {
                stablo.append(" ".repeat(dubina)).append("<").append(puniNaziviNeZavrsnihZnakova.get(stog.peek())).append(">\n");
                if (stog.peek().equals("EL") || stog.peek().equals("TL") || stog.peek().equals("LN")) {
                    stablo.append(" ".repeat(++dubina)).append("$\n");
                } else {
                    System.out.println("err kraj");
                    System.exit(0);
                }
                dubina--;
                stog.pop();
            } else if (zavrsniZnakovi.contains(stog.peek())) {
                stablo.append(" ".repeat(dubina)).append(stog.peek()).append("\n");
                stog.pop();
            }
        }

        System.out.println(stablo);
        System.exit(0);
    }
}


class Pair<T, U> {

    public final T first;
    public final U second;

    public Pair(T first, U second) {
        this.first = first;
        this.second = second;
    }
}


