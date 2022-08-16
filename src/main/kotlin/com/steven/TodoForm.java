package com.steven;


import org.jboss.resteasy.reactive.PartType;

import javax.ws.rs.FormParam;
import javax.ws.rs.core.MediaType;

public class TodoForm {

    public @FormParam("title") @PartType(MediaType.TEXT_PLAIN) String title;
    public @FormParam("completed") @PartType(MediaType.TEXT_PLAIN) String completed;
    public @FormParam("priority") @PartType(MediaType.TEXT_PLAIN) int priority;

    public Todo convertIntoTodo() {
//        Todo todo = new Todo();
//        todo.title = title;
//        todo.completed = "on".equals(completed);
//        todo.priority = priority;
        return new Todo(this);
    }

    public Todo updateTodo(Todo toUpdate) {
//        toUpdate.title = title;
//        toUpdate.completed = "on".equals(completed);
//        toUpdate.priority = priority;
        return new Todo(toUpdate, this);
    }
}
