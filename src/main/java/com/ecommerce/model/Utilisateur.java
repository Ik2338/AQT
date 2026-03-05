package com.ecommerce.model;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Entity @Table(name = "utilisateur")
public class Utilisateur {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @NotBlank @Column(nullable = false) private String nom;
    @NotBlank @Column(nullable = false) private String prenom;
    @NotBlank @Email @Column(nullable = false, unique = true) private String email;
    @NotBlank @Column(name = "mot_de_passe", nullable = false) private String motDePasse;
    @Column(nullable = false) private boolean actif = true;
    @Column(length = 20) private String telephone;
    @Column(length = 200) private String adresse;
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "utilisateur_role",
        joinColumns = @JoinColumn(name = "utilisateur_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles = new HashSet<>();
    public Utilisateur() {}
    public Utilisateur(String nom, String prenom, String email, String motDePasse) {
        this.nom=nom; this.prenom=prenom; this.email=email; this.motDePasse=motDePasse; this.actif=true;
    }
    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public String getNom() { return nom; } public void setNom(String nom) { this.nom = nom; }
    public String getPrenom() { return prenom; } public void setPrenom(String prenom) { this.prenom = prenom; }
    public String getEmail() { return email; } public void setEmail(String email) { this.email = email; }
    public String getMotDePasse() { return motDePasse; } public void setMotDePasse(String m) { this.motDePasse = m; }
    public boolean isActif() { return actif; } public void setActif(boolean actif) { this.actif = actif; }
    public String getTelephone() { return telephone; } public void setTelephone(String t) { this.telephone = t; }
    public String getAdresse() { return adresse; } public void setAdresse(String a) { this.adresse = a; }
    public Set<Role> getRoles() { return roles; } public void setRoles(Set<Role> roles) { this.roles = roles; }
}
