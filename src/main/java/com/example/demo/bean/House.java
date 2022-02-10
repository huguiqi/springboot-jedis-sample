package com.example.demo.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Created by sam on 2017/7/30.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class House implements Serializable {

    private String name,square,city;
}
