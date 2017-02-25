package ru.kabor.demand.prediction.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "v_sales_rest")
public class SalesRest {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    @Column(name = "whs_id", nullable = false)
    private int whsId;

    @Column(name = "art_id", nullable = false)
    private int artId;

    @Column(name = "day_id", nullable = false)
    private LocalDate dayId;

    @Column(name = "sale_qnty")
    private double saleQnty;

    @Column(name = "rest_qnty")
    private double restQnty;

    @ManyToOne(optional = false)
    @JoinColumn(name = "request_id", nullable = false)
    private Request request;
}
