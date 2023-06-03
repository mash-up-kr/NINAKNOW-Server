package kr.mashup.wequiz.application.quiz

import kr.mashup.wequiz.config.auh.UserInfoDto
import kr.mashup.wequiz.controller.quiz.model.CreateQuizRequest
import kr.mashup.wequiz.domain.quiz.Quiz
import kr.mashup.wequiz.domain.quiz.answer.Answer
import kr.mashup.wequiz.domain.quiz.question.Question
import kr.mashup.wequiz.repository.quiz.QuizRepository
import kr.mashup.wequiz.repository.user.UserRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class QuizService(
    private val userRepository: UserRepository,
    private val quizRepository: QuizRepository,
) {
    fun createQuiz(
        userInfoDto: UserInfoDto,
        createQuizRequest: CreateQuizRequest,
    ): Quiz {
        val user = userRepository.findByIdOrNull(userInfoDto.id) ?: throw RuntimeException()
        val quiz = Quiz.createNew(
            user = user,
            title = createQuizRequest.title,
        )

        val questions = createQuizRequest.questions.map { questionDto ->
            val question = Question.createNew(
                quiz = quiz,
                title = questionDto.title,
                priority = questionDto.priority,
                duplicatedAnswer = questionDto.duplicatedAnswer,
            )

            val answers = questionDto.answers.map { answerDto ->
                Answer.createNew(
                    question = question,
                    content = answerDto.content,
                    priority = answerDto.priority,
                    correctAnswer = answerDto.correctAnswer,
                )
            }
            question.also { it.setAnswers(answers) }
        }

        quiz.setQuestions(questions = questions)
        return quizRepository.save(quiz)
    }
}

