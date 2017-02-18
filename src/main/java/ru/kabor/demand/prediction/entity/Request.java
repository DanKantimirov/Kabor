package ru.kabor.demand.prediction.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Email;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

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
}
