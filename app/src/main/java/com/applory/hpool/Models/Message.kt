package com.applory.hpool.Models

class Message(val name: String, val content: String, val userId: String, val messageId: String) {
    //MessageId는 DB에는 Empty이고 나중에 채팅을 모두 지울 때 쓰기위한 Document id를 받아오는 용도
}