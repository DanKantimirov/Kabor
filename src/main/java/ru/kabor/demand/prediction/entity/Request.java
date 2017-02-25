package ru.kabor.demand.prediction.entity;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.validator.constraints.Email;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "v_request")
public class Request {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "request_id")
    private int id;

    @Column(length = 45, nullable = false)
    @Email
    private String email;

    @Column(name = "send_date_time", nullable = false)
    private LocalDateTime sendDateTime;

    @Column(nullable = false)
    private int status;

    @Column(name = "response_text", length = 500)
    private String responseText;

    @Column(name = "attachment_path", length = 150)
    private String attachmentPath;

    @Column(name = "document_path", length = 150, nullable = false)
    private String documentPath;

    @OneToMany(mappedBy = "request", targetEntity = SalesRest.class, cascade = CascadeType.ALL)
    Set<SalesRest> salesRest = new HashSet<>();

    @OneToOne(mappedBy = "request", targetEntity = ForecastParameter.class, cascade = CascadeType.ALL)
    ForecastParameter forecastParameter;
}
