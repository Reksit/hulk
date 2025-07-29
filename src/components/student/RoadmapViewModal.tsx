import React from 'react';
import { X, BookOpen, Calendar, Target, Clock } from 'lucide-react';
import { Task } from '../../types';

interface RoadmapViewModalProps {
  task: Task;
  onClose: () => void;
}

const RoadmapViewModal: React.FC<RoadmapViewModalProps> = ({ task, onClose }) => {
  const getRoadmapData = () => {
    try {
      return JSON.parse(task.roadmapData || '{}');
    } catch {
      return {};
    }
  };

  const roadmapData = getRoadmapData();
  const steps = roadmapData.steps || [];

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
      <div className="bg-white rounded-lg shadow-xl max-w-4xl w-full max-h-[90vh] overflow-y-auto">
        <div className="flex justify-between items-center p-6 border-b">
          <div className="flex items-center">
            <BookOpen className="w-6 h-6 text-purple-600 mr-3" />
            <div>
              <h2 className="text-2xl font-bold text-gray-900">{task.title}</h2>
              <p className="text-gray-600 mt-1">{task.description}</p>
            </div>
          </div>
          <button
            onClick={onClose}
            className="text-gray-400 hover:text-gray-600 transition-colors"
          >
            <X className="w-6 h-6" />
          </button>
        </div>

        <div className="p-6">
          {/* Roadmap Info */}
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mb-6">
            <div className="bg-purple-50 rounded-lg p-4">
              <div className="flex items-center">
                <Target className="w-5 h-5 text-purple-600 mr-2" />
                <div>
                  <p className="text-sm text-purple-600 font-medium">Domain</p>
                  <p className="text-purple-900 font-semibold">{roadmapData.domain || 'N/A'}</p>
                </div>
              </div>
            </div>
            
            <div className="bg-blue-50 rounded-lg p-4">
              <div className="flex items-center">
                <Clock className="w-5 h-5 text-blue-600 mr-2" />
                <div>
                  <p className="text-sm text-blue-600 font-medium">Timeframe</p>
                  <p className="text-blue-900 font-semibold">{roadmapData.timeframe || 'N/A'}</p>
                </div>
              </div>
            </div>
            
            <div className="bg-green-50 rounded-lg p-4">
              <div className="flex items-center">
                <Calendar className="w-5 h-5 text-green-600 mr-2" />
                <div>
                  <p className="text-sm text-green-600 font-medium">Status</p>
                  <p className="text-green-900 font-semibold">{task.status}</p>
                </div>
              </div>
            </div>
          </div>

          {/* Learning Steps */}
          <div>
            <h3 className="text-xl font-semibold text-gray-900 mb-4 flex items-center">
              <BookOpen className="w-5 h-5 mr-2 text-purple-600" />
              Learning Roadmap Steps
            </h3>
            
            {steps.length > 0 ? (
              <div className="space-y-4">
                {steps.map((step: string, index: number) => (
                  <div key={index} className="flex items-start group hover:bg-gray-50 p-4 rounded-lg transition-colors">
                    <div className="flex-shrink-0 w-8 h-8 bg-gradient-to-r from-purple-500 to-blue-600 text-white rounded-full flex items-center justify-center text-sm font-semibold mr-4">
                      {index + 1}
                    </div>
                    <div className="flex-1">
                      <p className="text-gray-800 leading-relaxed group-hover:text-gray-900">{step}</p>
                      <div className="mt-2 flex items-center text-xs text-gray-500">
                        <Clock className="w-3 h-3 mr-1" />
                        <span>Step {index + 1} of {steps.length}</span>
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            ) : (
              <div className="text-center py-8">
                <BookOpen className="w-12 h-12 text-gray-300 mx-auto mb-3" />
                <p className="text-gray-500">No roadmap steps available</p>
              </div>
            )}
          </div>

          {/* Task Timeline */}
          <div className="mt-8 bg-gray-50 rounded-lg p-4">
            <h4 className="font-medium text-gray-900 mb-3">Task Timeline</h4>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4 text-sm">
              <div>
                <span className="text-gray-600">Created:</span>
                <span className="ml-2 font-medium">{new Date(task.createdAt).toLocaleDateString()}</span>
              </div>
              <div>
                <span className="text-gray-600">Due Date:</span>
                <span className="ml-2 font-medium">{new Date(task.endDateTime).toLocaleDateString()}</span>
              </div>
              {task.completedAt && (
                <div>
                  <span className="text-gray-600">Completed:</span>
                  <span className="ml-2 font-medium text-green-600">{new Date(task.completedAt).toLocaleDateString()}</span>
                </div>
              )}
            </div>
          </div>
        </div>

        <div className="flex justify-end p-6 border-t">
          <button
            onClick={onClose}
            className="px-6 py-2 bg-gray-600 text-white rounded-lg hover:bg-gray-700 transition-colors"
          >
            Close
          </button>
        </div>
      </div>
    </div>
  );
};

export default RoadmapViewModal;