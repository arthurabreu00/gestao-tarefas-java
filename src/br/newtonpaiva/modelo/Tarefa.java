/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.newtonpaiva.modelo;

import java.nio.file.*;

import java.util.Calendar;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.LinkedList;

/**
 *
 * @author arthur
 */
public class Tarefa implements Comparable<Tarefa> {

    private Integer id;
    private String nome;
    private Integer prioridade;
    private Calendar dataLimite;
    private String situacao;
    private Integer percentual;
    private String descricao;

    private static final String HOST = "jdbc:mysql://localhost:3306/gestao_tarefas";
    private static final String NAME = "root";
    private static final String PASSWORD = "";

    public Tarefa() {
    }

    public Tarefa(String nome, Integer prioridade) {
        this.nome = nome;
        this.prioridade = prioridade;
    }

    public Tarefa(String nome, Integer prioridade, Calendar dataLimite, String situacao, Integer percentual, String descricao) {
        this.nome = nome;
        this.prioridade = prioridade;
        this.dataLimite = dataLimite;
        this.situacao = situacao;
        this.percentual = percentual;
        this.descricao = descricao;
    }

    public Tarefa(Integer id, String nome, Integer prioridade, Calendar dataLimite, String situacao, Integer percentual, String descricao) throws Exception {

        if (nome == null || nome.trim().isEmpty()) {
            throw new Exception("Nome invalido ou vazio");
        }

        if (prioridade == null) {
            throw new Exception("Prioridade invalida");
        }

        this.id = id;
        this.nome = nome;
        this.prioridade = prioridade;
        this.dataLimite = dataLimite;
        this.situacao = situacao;
        this.percentual = percentual;
        this.descricao = descricao;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public Integer getPrioridade() {
        return prioridade;
    }

    public void setPrioridade(Integer prioridade) {
        this.prioridade = prioridade;
    }

    public Calendar getDataLimite() {
        return dataLimite;
    }

    public void setDataLimite(Calendar dataLimite) {
        this.dataLimite = dataLimite;
    }

    public String getSituacao() {
        return situacao;
    }

    public void setSituacao(String situacao) {
        this.situacao = situacao;
    }

    public Integer getPercentual() {
        return percentual;
    }

    public void setPercentual(Integer percentual) {
        this.percentual = percentual;
    }

    public String getDescricao() {
        return descricao;
    }

    public String gerarLinhaCSV() {
        return this.nome + ";" + this.prioridade
                + ";" + this.situacao + ";" + this.percentual
                + ";" + this.descricao + ";\n";
    }

    public int salvar() throws SQLException {

        String rawQuery = "INSERT INTO "
                + "tarefa(nome,prioridade,data_limite,situacao,percentual,descricao)"
                + "values (?,?,?,?,?,?)";

        try (
                Connection conn = DriverManager.getConnection(
                        this.HOST,
                        this.NAME,
                        this.PASSWORD
                );
                PreparedStatement stm = conn.prepareStatement(rawQuery);) {
                    stm.setString(1, this.getNome());
                    stm.setInt(2, this.getPrioridade());
                    stm.setDate(3, new Date(getDataLimite().getTimeInMillis()));
                    stm.setString(4, this.getSituacao());
                    stm.setInt(5, this.getPercentual());
                    stm.setString(6, this.getDescricao());

                    int rows = stm.executeUpdate();
                    return rows;
                }
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public static boolean excluir(int id) throws SQLException {

        String rawQuery = "DELETE FROM tarefa WHERE id_tarefa=?";
        try (
                Connection conn = DriverManager.getConnection(
                        HOST,
                        NAME,
                        PASSWORD
                );
                PreparedStatement stm = conn.prepareStatement(rawQuery);) {
                    stm.setInt(1, id);

                    int rows = stm.executeUpdate();
                    return rows > 0;

                }

    }

    public static List<Tarefa> listar() throws SQLException {

        String query = "SELECT * FROM tarefa";

        try (
                Connection conn = DriverManager.getConnection(
                        HOST,
                        NAME,
                        PASSWORD
                );
                PreparedStatement stm = conn.prepareStatement(query);) {
                    ResultSet resultado = stm.executeQuery();

                    List<Tarefa> lista = new LinkedList<Tarefa>();

                    while (resultado.next()) {
                        Tarefa t = new Tarefa();
                        t.setId(resultado.getInt("id_tarefa"));
                        t.setNome(resultado.getString("nome"));
                        t.setPrioridade(resultado.getInt("prioridade"));
                        t.setSituacao(resultado.getString("situacao"));
                        t.setPercentual(resultado.getInt("percentual"));
                        t.setDescricao(resultado.getString("descricao"));

                        Date dataAux = resultado.getDate("data_limite");
                        Calendar dataLimite = Calendar.getInstance();
                        dataLimite.setTimeInMillis(dataAux.getTime());

                        t.setDataLimite(dataLimite);

                        lista.add(t);
                    }

                    return lista;
                }
    }

    private static Tarefa gerarTarefaFromLinhaCSV(String[] informacoes) throws NumberFormatException {
        Tarefa t = new Tarefa();
        t.setNome(informacoes[0]);
        t.setPrioridade(Integer.parseInt(informacoes[1]));
        t.setDataLimite(Calendar.getInstance());
        t.setSituacao(informacoes[2]);
        t.setPercentual(Integer.parseInt(informacoes[3]));
        t.setDescricao(informacoes[4]);
        return t;
    }

    public static List<Tarefa> listar(String filtro) throws Exception {;
        // Tarley preferi deixar desse modo em um primeiro momento.
        return listar()
                .stream()
                .filter(t -> !t.getNome().toUpperCase().contains(filtro.toUpperCase()))
                .collect(Collectors.toList());
    }

    @Override
    public int compareTo(Tarefa o) {
        String meuNome = getNome();
        String outroNome = o.getNome();

        return meuNome.toUpperCase().compareTo(outroNome.toUpperCase()) * -1;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Tarefa other = (Tarefa) obj;
        if (!Objects.equals(this.nome, other.nome)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 23 * hash + Objects.hashCode(this.nome);
        return hash;
    }

}
