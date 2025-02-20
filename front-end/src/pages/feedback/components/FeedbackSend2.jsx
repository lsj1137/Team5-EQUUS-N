import { useEffect, useState } from 'react';
import { useFeedbackObjective } from '../../../api/useFeedback';
import KeywordButton from '../../../components/buttons/KeywordButton';
import { showToast } from '../../../utility/handleToast';
import FooterWrapper from '../../../components/wrappers/FooterWrapper';
import LargeButton from '../../../components/buttons/LargeButton';
import { useLocation, useNavigate } from 'react-router-dom';
import { AnimatePresence, motion } from 'motion/react';

export default function FeedbackSend2() {
  const navigate = useNavigate();
  const locationState = useLocation().state;

  const { data: objectives } = useFeedbackObjective();

  const [selectedObjectives, setSelectedObjectives] = useState([]);
  const [step, setStep] = useState(2);

  useEffect(() => {
    setTimeout(() => {
      selectedObjectives.length > 0 &&
        navigate('../3', {
          state: {
            ...locationState,
            objectiveFeedbacks: selectedObjectives,
          },
        });
    }, 500);
  }, [step]);

  const onKeywordButtonClick = (keyword) => {
    selectedObjectives.includes(keyword) ?
      setSelectedObjectives([
        ...selectedObjectives.filter((item) => item !== keyword),
      ])
    : selectedObjectives.length < 5 ?
      setSelectedObjectives([...selectedObjectives, keyword])
    : showToast('키워드는 5개까지 선택 가능해요');
  };

  const containerVariants = {
    hidden: { opacity: 0 },
    show: {
      opacity: 1,
      transition: {
        staggerChildren: 0.1,
      },
    },
  };

  const itemVariants = {
    hidden: { opacity: 0, y: 20 },
    show: {
      opacity: 1,
      y: 0,
      transition: {
        type: 'spring',
        bounce: 0.32,
      },
    },
  };

  return (
    <div className='flex size-full flex-col gap-8'>
      <h1 className='header-2 text-gray-0 mt-3 whitespace-pre-line'>
        {'보낼 피드백 키워드를\n선택해 주세요'}
      </h1>
      <AnimatePresence>
        {step === 2 && objectives && (
          <motion.ul
            className='scrollbar-hidden flex w-full gap-8 overflow-x-auto p-1 whitespace-nowrap'
            variants={containerVariants}
            initial='hidden'
            animate='show'
          >
            {Object.keys(objectives[locationState.feedbackFeeling]).map(
              (title, index) => (
                <ObjectiveColumn
                  key={index}
                  title={title}
                  keywords={objectives[locationState.feedbackFeeling][title]}
                  selectedKeywords={selectedObjectives}
                  onClick={onKeywordButtonClick}
                  itemVariants={itemVariants}
                />
              ),
            )}
          </motion.ul>
        )}
      </AnimatePresence>
      <FooterWrapper>
        <LargeButton
          isOutlined={false}
          text='다음'
          disabled={selectedObjectives.length === 0}
          onClick={() => setStep(3)}
        />
      </FooterWrapper>
    </div>
  );
}

const ObjectiveColumn = ({
  title,
  keywords,
  selectedKeywords,
  onClick,
  itemVariants,
}) => {
  return (
    <motion.li
      className='inline-block w-80'
      variants={itemVariants}
      exit={{ opacity: 0, x: -30 }}
    >
      <h2 className='subtitle-1 text-gray-0 mb-3'>{title}</h2>
      <ul className='flex flex-col gap-2'>
        {keywords.map((keyword, index) => (
          <KeywordButton
            key={index}
            isActive={selectedKeywords.includes(keyword)}
            onClick={() => onClick(keyword)}
          >
            {keyword}
          </KeywordButton>
        ))}
      </ul>
    </motion.li>
  );
};
