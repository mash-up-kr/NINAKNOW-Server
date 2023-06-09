package kr.mashup.wequiz.application.quiz

import kr.mashup.wequiz.config.auh.UserInfoDto
import kr.mashup.wequiz.controller.quiz.model.CreateQuizRequest
import kr.mashup.wequiz.controller.quiz.model.GetQuizResponse
import kr.mashup.wequiz.domain.quiz.Quiz
import kr.mashup.wequiz.domain.quiz.answer.Answer
import kr.mashup.wequiz.domain.quiz.question.Question
import kr.mashup.wequiz.repository.quiz.QuizRepository
import kr.mashup.wequiz.repository.user.UserRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class QuizService(
    private val userRepository: UserRepository,
    private val quizRepository: QuizRepository,
) {
    @Transactional
    fun createQuiz(
        userInfoDto: UserInfoDto,
        createQuizRequest: CreateQuizRequest,
    ): Quiz {
        val user = userRepository.findByIdOrNull(userInfoDto.id) ?: throw IllegalArgumentException()
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

    @Transactional(readOnly = true)
    fun getQuiz(quizId: Long): GetQuizResponse {
        val quiz = quizRepository.findByIdOrNull(quizId) ?: throw IllegalArgumentException()
        return GetQuizResponse.from(quiz)
    }

    @Transactional
    fun deleteQuiz(requesterId: Long, quizId: Long) {
        val quiz = quizRepository.findByIdOrNull(quizId) ?: throw IllegalArgumentException()
        if (!quiz.isOwner(requesterId)) {
            throw RuntimeException("본인의 퀴즈만 삭제 할 수 있어요")
        }
        quiz.delete()
    }
}

