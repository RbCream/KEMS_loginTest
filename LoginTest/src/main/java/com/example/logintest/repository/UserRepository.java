package com.example.logintest.repository;

import com.example.logintest.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

//CRUD 함수를 기본적으로 JpaRepository가 가지고 있음
//@Repository 어노테이션 필요 없음. JpaRepository를 상속했기 때문에
public interface UserRepository extends JpaRepository<User, Integer> {
    //findBy 규칙 => Username 문법
    //select * from user where username = ?
    public User findByUsername(String username); //Jpa Query Method

    //select * from user where email = ?
    public User findByEmail(String email);
}
